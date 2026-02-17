import React from 'react';
import DOMPurify from 'dompurify';

export interface EmailBodyProps {
  body: string;
  bodyType?: 'html' | 'text';
  className?: string;
}

export const EmailBody: React.FC<EmailBodyProps> = ({
  body,
  bodyType,
  className = '',
}) => {
  if (!body) {
    return null;
  }

  // Determine if body is HTML: use bodyType if provided, otherwise detect from content
  const isHtml = bodyType === 'html' || (bodyType === undefined && /<[a-z][\s\S]*>/i.test(body));

  // Sanitize HTML using DOMPurify
  const sanitizeHtml = (html: string): string => {
    // Configure DOMPurify to allow common email HTML elements while removing dangerous content
    const config = {
      ALLOWED_TAGS: [
        'p', 'div', 'span', 'br', 'strong', 'em', 'u', 'b', 'i', 'a', 'img',
        'table', 'tr', 'td', 'th', 'tbody', 'thead', 'tfoot',
        'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
        'blockquote', 'pre', 'code', 'hr', 'font', 'center'
      ],
      ALLOWED_ATTR: [
        'href', 'src', 'alt', 'title', 'width', 'height', 'style',
        'class', 'id', 'align', 'valign', 'colspan', 'rowspan',
        'bgcolor', 'color', 'face', 'size', 'target'
      ],
      ALLOW_DATA_ATTR: false,
      KEEP_CONTENT: true,
    };

    let sanitized = DOMPurify.sanitize(html, config);

    // Add inline styles to prevent overflow for email-specific elements
    sanitized = sanitized.replace(
      /<body([^>]*)>/gi,
      '<body$1 style="max-width: 100%; word-wrap: break-word; overflow-wrap: anywhere;">'
    );
    sanitized = sanitized.replace(
      /<div([^>]*)>/gi,
      (match, attrs) => {
        // Only add style if it doesn't already have one
        if (!attrs || !attrs.includes('style=')) {
          return `<div${attrs} style="max-width: 100%; word-wrap: break-word; overflow-wrap: anywhere;">`;
        }
        return match;
      }
    );
    sanitized = sanitized.replace(
      /<table([^>]*)>/gi,
      (match, attrs) => {
        if (!attrs || !attrs.includes('style=')) {
          return `<table${attrs} style="max-width: 100%; table-layout: fixed;">`;
        }
        return match;
      }
    );

    return sanitized;
  };

  const displayBody = isHtml ? sanitizeHtml(body) : body;

  return (
    <div className={`prose prose-sm max-w-none break-words overflow-wrap-anywhere ${className}`}>
      {isHtml ? (
        <div
          className="email-body-html break-words overflow-wrap-anywhere"
          style={{
            wordBreak: 'break-word',
            overflowWrap: 'anywhere',
            maxWidth: '100%',
            // Preserve email styling while ensuring responsive layout
            lineHeight: '1.6',
          }}
          dangerouslySetInnerHTML={{ __html: displayBody }}
        />
      ) : (
        <div
          className="whitespace-pre-wrap text-slate-700 break-words overflow-wrap-anywhere"
          style={{
            wordBreak: 'break-word',
            overflowWrap: 'anywhere',
            maxWidth: '100%',
          }}
        >
          {displayBody}
        </div>
      )}
    </div>
  );
};
