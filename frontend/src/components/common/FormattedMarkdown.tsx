import React from "react";
import { cn } from "@/lib/utils";

interface FormattedMarkdownProps {
  content?: string | null;
  className?: string;
  theme?: "dark" | "light";
}

/**
 * Utility to unescape raw JSON or API-escaped strings
 */
export const cleanRawString = (str: any): string => {
  if (str === null || str === undefined) return "";
  if (typeof str !== "string") {
    try {
      return JSON.stringify(str, null, 2);
    } catch {
      return String(str);
    }
  }

  let cleaned = str;
  // Replace literal escaped newlines \n with actual newlines
  cleaned = cleaned.replace(/\\n/g, "\n");
  // Replace literal escaped tabs \t with spaces
  cleaned = cleaned.replace(/\\t/g, "  ");
  // Replace escaped quotes \" with "
  cleaned = cleaned.replace(/\\"/g, '"');
  // Replace escaped backslashes \\ with \
  cleaned = cleaned.replace(/\\\\/g, "\\");

  return cleaned.trim();
};

/**
 * Parses inline text for **bold**, *italic*, and `code` formatting
 */
const renderInlineFormatting = (text: string, isDark: boolean): React.ReactNode => {
  // Regex to match **bold** or `code` tokens
  const parts = text.split(/(\*\*.*?\*\*|`.*?`)/g);

  return parts.map((part, index) => {
    if (part.startsWith("**") && part.endsWith("**") && part.length >= 4) {
      const boldText = part.slice(2, -2);
      return (
        <strong
          key={index}
          className={cn("font-bold", isDark ? "text-slate-100" : "text-gray-900")}
        >
          {boldText}
        </strong>
      );
    }
    if (part.startsWith("`") && part.endsWith("`") && part.length >= 2) {
      const codeText = part.slice(1, -1);
      return (
        <code
          key={index}
          className={cn(
            "px-1.5 py-0.5 rounded font-mono text-[11px] font-semibold border",
            isDark
              ? "bg-slate-950 border-slate-800 text-primary"
              : "bg-primary-light border-primary-border text-primary"
          )}
        >
          {codeText}
        </code>
      );
    }
    return <span key={index}>{part}</span>;
  });
};

/**
 * FormattedMarkdown Component
 * Renders raw/escaped strings as clean structured HTML
 */
export const FormattedMarkdown = ({
  content,
  className,
  theme = "dark",
}: FormattedMarkdownProps) => {
  if (!content) return null;

  const isDark = theme === "dark";
  const cleaned = cleanRawString(content);

  // Check if content is a fenced code block
  if (cleaned.startsWith("```") && cleaned.endsWith("```")) {
    const lines = cleaned.slice(3, -3).trim().split("\n");
    const codeBody = lines.join("\n");
    return (
      <div
        className={cn(
          "p-3 rounded-xl border font-mono text-[11px] overflow-x-auto my-2",
          isDark
            ? "bg-slate-950 border-slate-800 text-slate-200"
            : "bg-gray-900 text-gray-100 border-gray-800",
          className
        )}
      >
        <pre className="whitespace-pre-wrap leading-relaxed">{codeBody}</pre>
      </div>
    );
  }

  // Check if content is valid JSON object/array
  if ((cleaned.startsWith("{") && cleaned.endsWith("}")) || (cleaned.startsWith("[") && cleaned.endsWith("]"))) {
    try {
      const parsed = JSON.parse(cleaned);
      return (
        <div
          className={cn(
            "p-3.5 rounded-xl border font-mono text-[11px] overflow-x-auto my-2",
            isDark
              ? "bg-slate-950 border-slate-800 text-slate-200"
              : "bg-slate-900 text-slate-100 border-slate-800",
            className
          )}
        >
          <pre className="whitespace-pre-wrap leading-relaxed">
            {JSON.stringify(parsed, null, 2)}
          </pre>
        </div>
      );
    } catch {
      // Not valid JSON, continue with line-by-line markdown parsing
    }
  }

  const lines = cleaned.split("\n");

  return (
    <div
      className={cn(
        "space-y-2 text-xs leading-relaxed",
        isDark ? "text-slate-300" : "text-gray-700",
        className
      )}
    >
      {lines.map((line, idx) => {
        const trimmed = line.trim();
        if (!trimmed) {
          return <div key={idx} className="h-1" />;
        }

        // Header ###
        if (trimmed.startsWith("### ")) {
          return (
            <h5
              key={idx}
              className={cn(
                "font-bold text-xs pt-1 uppercase tracking-wider",
                isDark ? "text-slate-100" : "text-gray-900"
              )}
            >
              {renderInlineFormatting(trimmed.replace(/^###\s+/, ""), isDark)}
            </h5>
          );
        }

        // Header ##
        if (trimmed.startsWith("## ")) {
          return (
            <h4
              key={idx}
              className={cn(
                "font-bold text-sm pt-1",
                isDark ? "text-slate-100" : "text-gray-900"
              )}
            >
              {renderInlineFormatting(trimmed.replace(/^##\s+/, ""), isDark)}
            </h4>
          );
        }

        // Header #
        if (trimmed.startsWith("# ")) {
          return (
            <h3
              key={idx}
              className={cn(
                "font-extrabold text-base pt-1",
                isDark ? "text-white" : "text-gray-900"
              )}
            >
              {renderInlineFormatting(trimmed.replace(/^#\s+/, ""), isDark)}
            </h3>
          );
        }

        // Bullet point: - or * or •
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
          const itemText = trimmed.replace(/^[-*•]\s+/, "");
          return (
            <div key={idx} className="flex items-start gap-2 pl-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-primary flex-shrink-0 mt-1.5" />
              <div className="flex-1">
                {renderInlineFormatting(itemText, isDark)}
              </div>
            </div>
          );
        }

        // Numbered list: 1. 2. etc.
        const numMatch = trimmed.match(/^(\d+)\.\s+(.*)$/);
        if (numMatch) {
          const num = numMatch[1];
          const itemText = numMatch[2];
          return (
            <div key={idx} className="flex items-start gap-2 pl-1.5">
              <span
                className={cn(
                  "font-bold font-mono text-[10px] w-4 h-4 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5",
                  isDark
                    ? "bg-purple-950/80 text-purple-300 border border-purple-800"
                    : "bg-purple-100 text-purple-700"
                )}
              >
                {num}
              </span>
              <div className="flex-1">
                {renderInlineFormatting(itemText, isDark)}
              </div>
            </div>
          );
        }

        // Regular paragraph line
        return (
          <p key={idx}>
            {renderInlineFormatting(trimmed, isDark)}
          </p>
        );
      })}
    </div>
  );
};
