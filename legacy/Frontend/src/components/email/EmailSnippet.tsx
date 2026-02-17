import React from 'react';

export interface EmailSnippetProps {
  snippet: string;
  maxLength?: number;
}

export const EmailSnippet: React.FC<EmailSnippetProps> = ({
  snippet,
  maxLength = 200,
}) => {
  const displayText = snippet.length > maxLength
    ? `${snippet.substring(0, maxLength)}...`
    : snippet;

  return (
    <p className="text-sm text-slate-600 line-clamp-2 break-words overflow-wrap-anywhere">
      {displayText}
    </p>
  );
};
