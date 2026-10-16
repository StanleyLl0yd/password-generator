#!/usr/bin/env python3
"""Restore protected signing material without interpreting Java-properties escapes."""
import base64
import os
from pathlib import Path


def properties_value(value):
    escaped = []
    encoded = value.encode('utf-16-be')
    escapes = {92: '\\\\', 10: '\\n', 13: '\\r', 9: '\\t', 32: '\\ '}
    for index in range(0, len(encoded), 2):
        unit = int.from_bytes(encoded[index:index + 2], 'big')
        escaped.append(escapes.get(unit, chr(unit) if 33 <= unit < 127 else f'\\u{unit:04x}'))
    return ''.join(escaped)


def configure():
    names = ('KEYSTORE_BASE64', 'KEYSTORE_PASSWORD', 'KEY_ALIAS', 'KEY_PASSWORD')
    values = {name: os.environ[name] for name in names}
    if not all(values.values()):
        raise SystemExit('Release signing inputs must be nonempty')
    keystore = Path(os.environ['RUNNER_TEMP']) / 'password-generator-release.jks'
    payload = base64.b64decode(values['KEYSTORE_BASE64'], validate=True)
    if not payload:
        raise SystemExit('Release keystore must be nonempty')
    keystore.write_bytes(payload)
    keystore.chmod(0o600)
    props = {
        'storeFile': str(keystore),
        'storePassword': values['KEYSTORE_PASSWORD'],
        'keyAlias': values['KEY_ALIAS'],
        'keyPassword': values['KEY_PASSWORD'],
    }
    path = Path('key.properties')
    path.write_text(''.join(f'{key}={properties_value(value)}\n' for key, value in props.items()), encoding='utf-8')
    path.chmod(0o600)


if __name__ == '__main__':
    configure()
