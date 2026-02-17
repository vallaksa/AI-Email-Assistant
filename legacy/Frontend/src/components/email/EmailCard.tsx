import React, { useState } from 'react';
import type { EmailDto } from '../../types';
import { Card } from '../common/Card';
import { Button } from '../common/Button';
import { EmailHeader } from './EmailHeader';
import { EmailSnippet } from './EmailSnippet';
import { EmailBody } from './EmailBody';
import { ReplySection } from '../reply/ReplySection';

export interface EmailCardProps {
  email: EmailDto;
  onReplySent?: () => void;
  onEmailClick?: (email: EmailDto) => void;
}

export const EmailCard: React.FC<EmailCardProps> = ({
  email,
  onReplySent,
  onEmailClick,
}) => {
  const [isReplyExpanded, setIsReplyExpanded] = useState(false);
  const [isBodyExpanded, setIsBodyExpanded] = useState(false);

  const handleReplyClick = () => {
    setIsReplyExpanded(!isReplyExpanded);
  };

  const handleBodyToggle = () => {
    setIsBodyExpanded(!isBodyExpanded);
  };

  const handleReplySent = () => {
    setIsReplyExpanded(false);
    onReplySent?.();
  };

  const handleCardClick = (e: React.MouseEvent) => {
    // Don't open panel if clicking on buttons or reply section
    const target = e.target as HTMLElement;
    if (
      target.closest('button') ||
      target.closest('.reply-section') ||
      target.closest('textarea') ||
      target.closest('input')
    ) {
      return;
    }
    onEmailClick?.(email);
  };

  return (
    <Card
      hover
      className="transition-all duration-200 cursor-pointer"
      onClick={handleCardClick}
    >
      <EmailHeader
        from={email.from}
        subject={email.subject}
        receivedAt={email.receivedAt}
      />
      
      {!isBodyExpanded ? (
        <EmailSnippet snippet={email.snippet} />
      ) : (
        <div className="mt-2 overflow-hidden break-words">
          <EmailBody body={email.body || email.snippet} bodyType={email.bodyType} />
        </div>
      )}
      
      <div className="mt-4 flex justify-between items-center">
        <Button
          variant="text"
          size="sm"
          onClick={handleBodyToggle}
          className="text-slate-600 hover:text-slate-800"
        >
          {isBodyExpanded ? 'Show Less' : 'Show Full Email'}
        </Button>
        <Button
          variant="text"
          size="sm"
          onClick={handleReplyClick}
          className="text-indigo-600 hover:text-indigo-700"
        >
          {isReplyExpanded ? 'Cancel' : 'Reply →'}
        </Button>
      </div>

      {isReplyExpanded && (
        <div className="mt-4 pt-4 border-t border-slate-200 reply-section">
          <ReplySection
            email={email}
            onReplySent={handleReplySent}
            onCancel={() => setIsReplyExpanded(false)}
          />
        </div>
      )}
    </Card>
  );
};
