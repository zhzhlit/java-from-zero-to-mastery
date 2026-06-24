import assert from 'node:assert/strict'
import {
  mkdir,
  mkdtemp,
  readdir,
  readFile,
  rm,
  stat,
  writeFile
} from 'node:fs/promises'
import os from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url))
const repositoryRoot = path.resolve(scriptDirectory, '../..')
const docsRoot = path.join(repositoryRoot, 'docs')

async function collectMarkdownFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true })
  const files = []

  for (const entry of entries) {
    const entryPath = path.join(directory, entry.name)

    if (entry.isDirectory()) {
      files.push(...await collectMarkdownFiles(entryPath))
    } else if (entry.isFile() && entry.name.endsWith('.md')) {
      files.push(entryPath)
    }
  }

  return files
}

function codeFreeLines(content) {
  const lines = content.split('\n')
  const result = []
  let fence = null

  for (const line of lines) {
    const marker = line.match(/^ {0,3}(`{3,}|~{3,})/)

    if (fence !== null) {
      if (
        marker
        && marker[1][0] === fence.character
        && marker[1].length >= fence.length
      ) {
        fence = null
      }
      result.push('')
      continue
    }

    if (marker) {
      fence = { character: marker[1][0], length: marker[1].length }
      result.push('')
      continue
    }

    if (/^(?: {4}|\t)/.test(line)) {
      result.push('')
      continue
    }

    result.push(removeInlineCode(line))
  }

  return result
}

function removeInlineCode(line) {
  let result = ''
  let index = 0

  while (index < line.length) {
    if (line[index] !== '`') {
      result += line[index]
      index += 1
      continue
    }

    const delimiterStart = index
    while (line[index] === '`') {
      index += 1
    }
    const delimiter = line.slice(delimiterStart, index)
    const closingIndex = line.indexOf(delimiter, index)

    if (closingIndex === -1) {
      result += delimiter
      continue
    }

    result += ' '.repeat(closingIndex + delimiter.length - delimiterStart)
    index = closingIndex + delimiter.length
  }

  return result
}

function isEscaped(text, index) {
  let backslashes = 0

  for (let cursor = index - 1; cursor >= 0 && text[cursor] === '\\'; cursor -= 1) {
    backslashes += 1
  }

  return backslashes % 2 === 1
}

function findClosingBracket(text, openingIndex) {
  let depth = 1

  for (let index = openingIndex + 1; index < text.length; index += 1) {
    if (isEscaped(text, index)) {
      continue
    }

    if (text[index] === '[') {
      depth += 1
    } else if (text[index] === ']') {
      depth -= 1
      if (depth === 0) {
        return index
      }
    }
  }

  return -1
}

function parseDestination(text, openingIndex) {
  let index = openingIndex + 1

  while (index < text.length && /[ \t]/.test(text[index])) {
    index += 1
  }

  if (text[index] === '<') {
    let target = ''

    for (index += 1; index < text.length; index += 1) {
      if (text[index] === '>' && !isEscaped(text, index)) {
        index += 1
        while (index < text.length && /[ \t]/.test(text[index])) {
          index += 1
        }
        return text[index] === ')' ? { target, end: index } : null
      }
      target += text[index]
    }

    return null
  }

  let depth = 1
  let target = ''

  for (; index < text.length; index += 1) {
    const character = text[index]

    if (isEscaped(text, index)) {
      target += character
      continue
    }

    if (character === '(') {
      depth += 1
      target += character
      continue
    }

    if (character === ')') {
      depth -= 1
      if (depth === 0) {
        return { target, end: index }
      }
      target += character
      continue
    }

    if ((character === ' ' || character === '\t') && depth === 1) {
      while (index < text.length && text[index] !== ')') {
        index += 1
      }
      return index < text.length ? { target, end: index } : null
    }

    target += character
  }

  return null
}

function extractInlineLinks(content) {
  const links = []

  for (const line of codeFreeLines(content)) {
    for (let index = 0; index < line.length; index += 1) {
      if (line[index] !== '[' || isEscaped(line, index)) {
        continue
      }

      // Images are intentionally outside the scope of this link checker.
      if (index > 0 && line[index - 1] === '!' && !isEscaped(line, index - 1)) {
        continue
      }

      const closingBracket = findClosingBracket(line, index)
      if (closingBracket === -1) {
        continue
      }

      let destinationStart = closingBracket + 1
      while (destinationStart < line.length && /[ \t]/.test(line[destinationStart])) {
        destinationStart += 1
      }

      if (line[destinationStart] !== '(') {
        index = closingBracket
        continue
      }

      const destination = parseDestination(line, destinationStart)
      if (destination !== null) {
        links.push(destination.target)
        index = destination.end
      } else {
        index = closingBracket
      }
    }
  }

  return links
}

async function isFile(candidate) {
  try {
    return (await stat(candidate)).isFile()
  } catch {
    return false
  }
}

function isWithinDocsRoot(candidate, root) {
  const relative = path.relative(root, candidate)
  return (
    relative === ''
    || (!relative.startsWith(`..${path.sep}`)
      && relative !== '..'
      && !path.isAbsolute(relative))
  )
}

async function targetExists(candidate) {
  if (await isFile(candidate)) {
    return true
  }

  if (path.extname(candidate) === '' && await isFile(`${candidate}.md`)) {
    return true
  }

  return await isFile(path.join(candidate, 'index.md'))
}

function localTarget(link) {
  if (/^(?:https?:|mailto:)/i.test(link) || link.startsWith('#')) {
    return null
  }

  const withoutQueryOrHash = link.split(/[?#]/, 1)[0]
  if (withoutQueryOrHash === '') {
    return null
  }

  try {
    return decodeURIComponent(withoutQueryOrHash)
  } catch {
    return withoutQueryOrHash
  }
}

async function checkDocs(root) {
  const markdownFiles = (await collectMarkdownFiles(root)).sort()
  const brokenLinks = []

  for (const sourceFile of markdownFiles) {
    const content = await readFile(sourceFile, 'utf8')

    for (const link of extractInlineLinks(content)) {
      const target = localTarget(link)
      if (target === null) {
        continue
      }

      const resolvedTarget = target.startsWith('/')
        ? path.resolve(root, `.${target}`)
        : path.resolve(path.dirname(sourceFile), target)

      if (
        !isWithinDocsRoot(resolvedTarget, root)
        || !await targetExists(resolvedTarget)
      ) {
        brokenLinks.push({
          source: path.relative(root, sourceFile),
          target: link
        })
      }
    }
  }

  return { fileCount: markdownFiles.length, brokenLinks }
}

async function runSelfTest() {
  assert.deepEqual(
    extractInlineLinks(String.raw`[escaped \] text](guide.md)`),
    ['guide.md'],
    'supports escaped closing brackets in link text'
  )
  assert.deepEqual(
    extractInlineLinks('[nested](a(b).md)'),
    ['a(b).md'],
    'supports balanced parentheses in link targets'
  )
  assert.deepEqual(
    extractInlineLinks('    [indented](missing.md)\n\t[tabbed](missing.md)'),
    [],
    'ignores indented code blocks'
  )
  assert.deepEqual(
    extractInlineLinks('`[inline](missing.md)`\n```\n[fenced](missing.md)\n```'),
    [],
    'ignores inline and fenced code'
  )
  assert.deepEqual(
    extractInlineLinks('![ignored](missing.png)'),
    [],
    'ignores image links'
  )

  const fixtureBase = await mkdtemp(path.join(os.tmpdir(), 'check-links-'))
  const fixtureRoot = path.join(fixtureBase, 'docs')

  try {
    await mkdir(path.join(fixtureRoot, 'section'), { recursive: true })
    await mkdir(path.join(fixtureRoot, 'readme-only'), { recursive: true })
    await writeFile(path.join(fixtureBase, 'outside.md'), '# Outside\n')
    await writeFile(path.join(fixtureRoot, 'good.md'), '# Good\n')
    await writeFile(path.join(fixtureRoot, 'a(b).md'), '# Nested\n')
    await writeFile(path.join(fixtureRoot, 'section', 'index.md'), '# Index\n')
    await writeFile(path.join(fixtureRoot, 'readme-only', 'README.md'), '# README\n')
    await writeFile(
      path.join(fixtureRoot, 'happy.md'),
      [
        '[file](./good.md)',
        '[extensionless](./good)',
        '[root relative](/good.md)',
        '[directory index](./section/)',
        '[nested](./a(b).md)',
        '[query](./good.md?view=full)',
        '[hash](./good.md#heading)',
        '[anchor](#heading)',
        '[query only](?view=full)',
        '[web](https://example.com)',
        '[mail](mailto:test@example.com)',
        '    [indented](./missing.md)',
        '\t[tabbed](./missing.md)',
        '`[inline](./missing.md)`',
        '```',
        '[fenced](./missing.md)',
        '```'
      ].join('\n')
    )

    const happyResult = await checkDocs(fixtureRoot)
    assert.deepEqual(happyResult.brokenLinks, [], 'accepts valid links')

    await writeFile(
      path.join(fixtureRoot, 'broken.md'),
      [
        '[missing](./does-not-exist.md)',
        '[README is not an index](./readme-only/)',
        '[escape](../outside.md)'
      ].join('\n')
    )

    const brokenResult = await checkDocs(fixtureRoot)
    assert.deepEqual(
      brokenResult.brokenLinks.map(({ target }) => target).sort(),
      ['./does-not-exist.md', './readme-only/', '../outside.md'].sort(),
      'rejects missing, README-only directory, and escaping targets'
    )
  } finally {
    await rm(fixtureBase, { recursive: true, force: true })
  }

  console.log('Self-test passed.')
}

if (process.argv.includes('--self-test')) {
  await runSelfTest()
} else {
  const result = await checkDocs(docsRoot)

  for (const brokenLink of result.brokenLinks) {
    console.error(`Broken link: docs/${brokenLink.source} -> ${brokenLink.target}`)
  }

  console.log(
    `Checked ${result.fileCount} Markdown files; `
      + `${result.brokenLinks.length} broken links.`
  )

  if (result.brokenLinks.length > 0) {
    process.exitCode = 1
  }
}
