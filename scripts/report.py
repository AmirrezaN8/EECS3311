#!/usr/bin/env python3
"""Render the report using Python's standard library and installed Chrome/Chromium."""
import argparse
import html
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
from urllib.parse import urlparse

REPO = 'https://github.com/AmirrezaN8/EECS3311/blob/main/'


def target(url, source, image=False):
    if urlparse(url).scheme or url.startswith('#'):
        return url
    path = (source.parent / url).resolve()
    if image:
        if not path.is_file():
            raise ValueError(f'Missing report image: {path}')
        return path.as_uri()
    return REPO + path.relative_to(Path.cwd().resolve()).as_posix()


def inline(text, source):
    pattern = r'!\[([^\]]*)\]\(([^)]+)\)|\[([^\]]+)\]\(([^)]+)\)|`([^`]+)`|\*\*([^*]+)\*\*'
    out, end = [], 0
    for m in re.finditer(pattern, text):
        out.append(html.escape(text[end:m.start()]))
        if m[1] is not None:
            url = target(m[2], source, True)
            out.append(f'<img alt="{html.escape(m[1])}" src="{html.escape(url, quote=True)}">')
        elif m[3] is not None:
            out.append(f'<a href="{html.escape(target(m[4], source), quote=True)}">{inline(m[3], source)}</a>')
        elif m[5] is not None:
            out.append(f'<code>{html.escape(m[5])}</code>')
        else:
            out.append(f'<strong>{html.escape(m[6])}</strong>')
        end = m.end()
    out.append(html.escape(text[end:]))
    return ''.join(out)


def render(source):
    lines, blocks, i = source.read_text().splitlines(), [], 0
    while i < len(lines):
        line = lines[i]
        if not line.strip():
            i += 1
            continue
        if line.startswith('```'):
            code = []
            i += 1
            while i < len(lines) and not lines[i].startswith('```'):
                code.append(lines[i]); i += 1
            blocks.append('<pre>' + html.escape('\n'.join(code)) + '</pre>')
            i += 1
        elif line.startswith('|'):
            rows = []
            while i < len(lines) and lines[i].startswith('|'):
                cells = lines[i].strip().strip('|').split('|')
                if not all(re.fullmatch(r'\s*:?-+:?\s*', c) for c in cells):
                    rows.append(cells)
                i += 1
            blocks.append('<table><thead><tr>' + ''.join('<th>'+inline(c.strip(), source)+'</th>' for c in rows[0]) + '</tr></thead><tbody>' + ''.join('<tr>'+''.join('<td>'+inline(c.strip(), source)+'</td>' for c in row)+'</tr>' for row in rows[1:]) + '</tbody></table>')
        elif line.startswith('#'):
            level = len(line)-len(line.lstrip('#'))
            blocks.append(f'<h{level}>'+inline(line[level:].strip(), source)+f'</h{level}>')
            i += 1
        elif re.match(r'^(?:- |\d+\. )', line):
            ordered = bool(re.match(r'^\d+\.', line))
            tag = 'ol' if ordered else 'ul'
            items = []
            while i < len(lines) and re.match(r'^\d+\. ' if ordered else r'^- ', lines[i]):
                items.append('<li>'+inline(re.sub(r'^(?:- |\d+\. )', '', lines[i]), source)+'</li>'); i += 1
            blocks.append(f'<{tag}>'+''.join(items)+f'</{tag}>')
        elif line.startswith('![F10 planning'):
            # Two wide sheets retain readable sequence labels instead of shrinking to A4.
            url = html.escape(target('implemented-sequence.png', source, True), quote=True)
            for part in range(2):
                blocks.append(f'<section class="sequence"><h3>F10 sequence — part {part+1} of 2</h3><div class="crop"><img style="top:-{part*215}mm" src="{url}"></div></section>')
            i += 1
        else:
            paragraph = [line]
            i += 1
            while i < len(lines) and lines[i].strip() and not re.match(r'^(?:#|\||```|!\[|- |\d+\. )', lines[i]):
                paragraph.append(lines[i]); i += 1
            blocks.append('<p>'+inline(' '.join(paragraph), source)+'</p>')
    return '''<!doctype html><html><head><meta charset="utf-8"><title>EECS3311 Stage 2 Implementation Report</title><style>
@page {size:A4; margin:17mm 16mm 18mm; @bottom-center {content:"EECS3311 · Stage 2 · " counter(page); font:9pt Arial; color:#526075}}
@page sequence {size:A3 landscape; margin:12mm 10mm;}
body {font:10pt/1.45 Arial,sans-serif;color:#202b3a} h1 {font-size:25pt;line-height:1.15;color:#16324f} h2 {font-size:18pt;color:#16324f;break-before:page} h3 {font-size:13pt;color:#234c68} h4 {font-size:11pt} h1,h2,h3,h4 {break-after:avoid} p {orphans:3;widows:3} a {color:#165987;text-decoration:none;overflow-wrap:anywhere} code {font:8.5pt monospace;overflow-wrap:anywhere} table {width:100%;border-collapse:collapse;font-size:8pt;line-height:1.35;margin:10pt 0;table-layout:fixed} th,td {padding:6pt;border:0.5pt solid #c7d2de;text-align:left;vertical-align:top;overflow-wrap:anywhere} th {background:#e7eef5;color:#16324f} tr {break-inside:avoid} thead {display:table-header-group} img {display:block;max-width:100%;height:auto;margin:12pt auto} pre {white-space:pre-wrap;background:#eef2f6;padding:10pt;font-size:8pt} li {margin-bottom:4pt} .sequence {page:sequence;break-before:page;break-after:page} .sequence h3 {margin:0 0 6mm} .crop {height:235mm;width:400mm;overflow:hidden;position:relative} .crop img {position:absolute;width:400mm;max-width:none;margin:0;left:0}
</style></head><body>''' + '\n'.join(blocks) + '</body></html>'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source', nargs='?', default='docs/stage2-report.md')
    parser.add_argument('output', nargs='?', default='stage2-report.pdf')
    args = parser.parse_args()
    source, output = Path(args.source).resolve(), Path(args.output).resolve()
    chrome = os.environ.get('CHROME') or shutil.which('google-chrome') or shutil.which('chromium')
    if not chrome:
        chrome = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
    if not Path(chrome).is_file():
        parser.error('Install Chrome/Chromium or set CHROME to its executable path.')
    with tempfile.TemporaryDirectory(prefix='eecs3311-report-') as tmp:
        document = Path(tmp)/'report.html'
        document.write_text(render(source))
        subprocess.run([chrome, '--headless', '--disable-gpu', '--no-pdf-header-footer', '--allow-file-access-from-files', '--no-first-run', '--no-default-browser-check', '--user-data-dir='+str(Path(tmp)/'profile'), '--print-to-pdf='+str(output), document.as_uri()], check=True, stdout=subprocess.DEVNULL)
    if not output.read_bytes().startswith(b'%PDF-'):
        raise ValueError('Renderer did not produce a valid PDF header')
    print(f'Generated {output} ({output.stat().st_size:,} bytes)')


if __name__ == '__main__':
    main()
