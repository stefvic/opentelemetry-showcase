import base64
import copy
import jsonpatch
import os
from flask import Flask, request, jsonify

OTEL_ENV = {
  'OTEL_TRACES_EXPORTER': os.environ['OTEL_TRACES_EXPORTER'],
  'OTEL_METRICS_EXPORTER': os.environ['OTEL_METRICS_EXPORTER'],
  'OTEL_EXPORTER_OTLP_ENDPOINT': os.environ['OTEL_EXPORTER_OTLP_ENDPOINT'],
  'OTEL_TRACES_SAMPLER': 'always_on'
}

JAVA_AGENT = ' -javaagent:/mnt/auto-trace/opentelemetry-javaagent.jar'

app = Flask(__name__)


@app.route('/decorate', methods=['POST'])
def decorate():
  payload = request.get_json()
  req = payload['request']
  source = req['object']
  target = copy.deepcopy(source)

  add_volume(target)
  add_init_container(target)
  tweak_containers(target)

  patch = jsonpatch.JsonPatch.from_diff(source, target)
  print(patch)

  return jsonify({
    'response': {
      'uid': req['uid'],
      'allowed': True,
      'patchType': 'JSONPatch',
      'patch': base64.b64encode(str(patch).encode()).decode(),

    }
  })


def tweak_containers(target):
  containers = target['spec'].get('containers', [])
  for container in containers:
    add_mount(container)
    edit_env(container)


def edit_env(container):
  env = container.get('env', [])
  for key, val in OTEL_ENV.items():
    env.append({
      'name': key,
      'value': val
    })
  # env.append({
  #     'name': 'JAEGER_SERVICE_NAME',
  #     'value': container['name']
  # })

  add_java_agent(env)

  container['env'] = env


def add_java_agent(env):
  existing = [e for e in env if e['name'] == 'JAVA_TOOL_OPTIONS']
  if existing:
    existing = existing[0]
    existing['value'] = existing['value'] + JAVA_AGENT
  else:
    env.append({
      'name': 'JAVA_TOOL_OPTIONS',
      'value': JAVA_AGENT
    })


def add_mount(container):
  mounts = container.get('volumeMounts', [])
  mounts.append({
    'mountPath': '/mnt/auto-trace',
    'name': 'auto-trace-mount'
  })
  container['volumeMounts'] = mounts


def add_init_container(target):
  inits = target['spec'].get('initContainers', [])
  inits.append({
    'name': 'autotrace-additions',
    'image': 'stefvic/auto-tracing-sidecar:latest',
    'volumeMounts': [{
      'mountPath': '/mnt/shared',
      'name': 'auto-trace-mount'
    }]
  })
  target['spec']['initContainers'] = inits


def add_volume(target):
  volumes = target['spec'].get('volumes', [])
  volumes.append({
    'name': 'auto-trace-mount',
    'emptyDir': {}
  })
  target['spec']['volumes'] = volumes


if __name__ == "__main__":
  app.run('0.0.0.0', debug=False,
          ssl_context=('webhook-server-tls.crt', 'webhook-server-tls.key'))
