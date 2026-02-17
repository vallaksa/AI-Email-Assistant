import React from 'react';
import type { ThreadMessageDto } from '../../types';
import { ThreadMessage } from './ThreadMessage';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { ErrorMessage } from '../common/ErrorMessage';

export interface EmailThreadProps {
  messages: ThreadMessageDto[];
  currentEmailId?: string;
  isLoading?: boolean;
  error?: string | null;
  onRetry?: () => void;
}

export const EmailThread: React.FC<EmailThreadProps> = ({
  messages,
  currentEmailId,
  isLoading = false,
  error = null,
  onRetry,
}) => {
  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-8">
        <LoadingSpinner size="md" />
      </div>
    );
  }

  if (error) {
    return (
      <ErrorMessage
        message={error}
        onRetry={onRetry}
        className="mb-4"
      />
    );
  }

  if (messages.length === 0) {
    return (
      <div className="text-center py-8 text-slate-500">
        <p>No messages in this thread.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold text-slate-900 mb-4">Conversation</h3>
      {messages.map((message) => (
        <ThreadMessage
          key={message.emailId}
          message={message}
          isCurrentEmail={message.emailId === currentEmailId}
        />
      ))}
    </div>
  );
};
