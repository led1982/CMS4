import { marked } from "marked";

export function MarkdownPreview({ content }: { content: string }) {
  return <div className="markdown-preview" dangerouslySetInnerHTML={{ __html: marked.parse(content) as string }} />;
}
