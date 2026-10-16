#!/usr/bin/env python3
"""Validate the metadata and PNG requirements used for the Play submission kit."""
from pathlib import Path
import re
import struct

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'store-assets/google-play'


def section(text, name):
    match = re.search(r'^## ' + re.escape(name) + r'\n+(.*?)(?=\n## |\Z)', text, re.M | re.S)
    if not match:
        raise ValueError(f'Missing listing field: {name}')
    return match[1].strip()


def verify_png(path, size, color_type):
    header = path.read_bytes()[:33]
    if header[:8] != b'\x89PNG\r\n\x1a\n' or header[12:16] != b'IHDR':
        raise ValueError(f'Invalid PNG: {path}')
    width, height, depth, color = struct.unpack('>IIBB', header[16:26])
    if (width, height) != size or depth != 8 or color != color_type:
        raise ValueError(f'Unexpected PNG dimensions/format: {path}')


def main():
    version = (ROOT / 'VERSION').read_text().strip()
    if (ASSETS / 'release-candidate').read_text().strip() != 'v' + version:
        raise ValueError('Play candidate marker does not match VERSION')
    for language in ('en-US', 'ru-RU'):
        text = (ASSETS / f'listing.{language}.md').read_text()
        for name, limit in [('Title', 30), ('Short description', 80), ('Full description', 4000), ('Release notes', 500)]:
            length = len(section(text, name))
            if not 0 < length <= limit:
                raise ValueError(f'{language}: {name} length {length}, limit {limit}')
        verify_png(ASSETS / f'feature-graphic-{language}.png', (1024, 500), 2)
    verify_png(ROOT / 'store-assets/app-icon-512.png', (512, 512), 6)
    print(f'Google Play listing lengths and image formats: OK ({version})')


if __name__ == '__main__':
    main()
