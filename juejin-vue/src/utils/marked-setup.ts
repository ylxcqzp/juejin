import { marked } from 'marked'
import hljs from 'highlight.js'

const renderer = new marked.Renderer()
renderer.code = function (token: { text: string; lang?: string }) {
  const lang = token.lang || ''
  const code =
    lang && hljs.getLanguage(lang)
      ? hljs.highlight(token.text, { language: lang }).value
      : hljs.highlightAuto(token.text).value
  return `<pre><code class="hljs language-${lang}">${code}</code></pre>`
}

renderer.image = function (token: { href: string; title: string | null; text: string }) {
  return `<img src="${token.href}" alt="${token.text}" loading="lazy" style="max-width:100%;border-radius:0.75rem;margin:1rem 0" />`
}

marked.setOptions({
  breaks: true,
  gfm: true,
  renderer,
})

export { marked }
