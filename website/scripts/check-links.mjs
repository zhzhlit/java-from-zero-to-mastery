import { readdir, readFile, stat } from 'node:fs/promises'
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

function removeCode(content) {
  const lines = content.split('\n')
  let fence = null

  return lines.map((line) => {
    const marker = line.match(/^ {0,3}(`{3,}|~{3,})/)

    if (marker && fence === null) {
      fence = { character: marker[1][0], length: marker[1].length }
      return ''
    }

    if (
      marker
      && fence !== null
      && marker[1][0] === fence.character
      && marker[1].length >= fence.length
    ) {
      fence = null
      return ''
    }

    if (fence !== null) {
      return ''
    }

    return line.replace(/(`+)(.*?)\1/g, '')
  }).join('\n')
}

function extractInlineLinks(content) {
  const links = []
  const linkPattern = /(?<!!)\[[^\]\n]*\]\(\s*(<[^>\n]+>|[^)\s]+)(?:\s+["'(][^)\n]*[")'])?\s*\)/g

  for (const match of removeCode(content).matchAll(linkPattern)) {
    const rawTarget = match[1]
    links.push(rawTarget.startsWith('<') ? rawTarget.slice(1, -1) : rawTarget)
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

async function targetExists(candidate) {
  if (await isFile(candidate)) {
    return true
  }

  if (path.extname(candidate) === '' && await isFile(`${candidate}.md`)) {
    return true
  }

  if (
    path.basename(candidate).toLowerCase() === 'index'
    && await isFile(path.join(path.dirname(candidate), 'README.md'))
  ) {
    return true
  }

  return (
    await isFile(path.join(candidate, 'index.md'))
    || await isFile(path.join(candidate, 'README.md'))
  )
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

const markdownFiles = (await collectMarkdownFiles(docsRoot)).sort()
const brokenLinks = []

for (const sourceFile of markdownFiles) {
  const content = await readFile(sourceFile, 'utf8')

  for (const link of extractInlineLinks(content)) {
    const target = localTarget(link)
    if (target === null) {
      continue
    }

    const resolvedTarget = target.startsWith('/')
      ? path.resolve(docsRoot, `.${target}`)
      : path.resolve(path.dirname(sourceFile), target)

    if (!await targetExists(resolvedTarget)) {
      brokenLinks.push({
        source: path.relative(repositoryRoot, sourceFile),
        target: link
      })
    }
  }
}

for (const brokenLink of brokenLinks) {
  console.error(`Broken link: ${brokenLink.source} -> ${brokenLink.target}`)
}

console.log(
  `Checked ${markdownFiles.length} Markdown files; ${brokenLinks.length} broken links.`
)

if (brokenLinks.length > 0) {
  process.exitCode = 1
}
