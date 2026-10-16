#!/usr/bin/env python3
"""Query OSV for the resolved production, test and build dependencies."""
import json
from pathlib import Path
import urllib.request


def queries(lines):
    result = []
    for coordinate in sorted(set(lines)):
        group, name, version = coordinate.split(':')
        result.append({'package': {'ecosystem': 'Maven', 'name': f'{group}:{name}'}, 'version': version})
    if not result:
        raise ValueError('Resolved dependency inventory is empty')
    return result


def main():
    path = Path('app/build/reports/dependencies/coordinates.txt')
    inventory = queries(path.read_text().splitlines())
    findings = []
    for start in range(0, len(inventory), 500):
        batch = inventory[start:start + 500]
        request = urllib.request.Request(
            'https://api.osv.dev/v1/querybatch',
            data=json.dumps({'queries': batch}).encode(),
            headers={'Content-Type': 'application/json'},
        )
        with urllib.request.urlopen(request, timeout=90) as response:
            results = json.load(response)['results']
        if len(results) != len(batch):
            raise ValueError('OSV returned an incomplete response')
        for dependency, result in zip(batch, results):
            for vulnerability in result.get('vulns', []):
                findings.append({'dependency': dependency, 'id': vulnerability['id']})
    path.with_name('osv.json').write_text(json.dumps({'scanned': len(inventory), 'findings': findings}, indent=2))
    if findings:
        for finding in findings:
            print(finding['dependency'], finding['id'])
        raise SystemExit('OSV found known vulnerabilities; review and remediate before release')
    print(f'OSV: {len(inventory)} resolved Maven coordinates checked, no known vulnerabilities returned')


if __name__ == '__main__':
    main()
