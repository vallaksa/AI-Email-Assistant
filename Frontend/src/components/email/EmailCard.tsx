import React, { useState } from 'react';
import type { EmailDto } from '../../types';
import { Card } from '../common/Card';
import { Button } from '../common/Button';
import { EmailHeader } from './EmailHeader';
import { EmailSnippet } from './EmailSnippet';
import { ReplySection } from '../reply/ReplySection';

export interface EmailCardProps {
  email: EmailDto;
  onReplySent?: () => void;
}

export const EmailCard: React.FC<EmailCardProps> = ({
  email,
  onReplySent,
}) => {
  const [isReplyExpanded, setIsReplyExpanded] = useState(false);

  const handleReplyClick = () => {
    setIsReplyExpanded(!isReplyExpanded);
  };

  const handleReplySent = () => {
    setIsReplyExpanded(false);
    onReplySent?.();
  };

  return (
    <Card hover className="transition-all duration-200">
      <EmailHeader
        from={email.from}
        subject={email.subject}
        receivedAt={email.receivedAt}
      />
      <EmailSnippet snippet={email.snippet} />
      
      <div className="mt-4 flex justify-end">
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
        <div className="mt-4 pt-4 border-t border-slate-200">
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
