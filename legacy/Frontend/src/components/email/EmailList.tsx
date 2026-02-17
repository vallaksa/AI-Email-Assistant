import React from 'react';
import type { EmailDto } from '../../types';
import { EmailCard } from './EmailCard';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { ErrorMessage } from '../common/ErrorMessage';

export interface EmailListProps {
  emails: EmailDto[];
  isLoading?: boolean;
  error?: string | null;
  onRetry?: () => void;
  onReplySent?: () => void;
  onEmailClick?: (email: EmailDto) => void;
}

export const EmailList: React.FC<EmailListProps> = ({
  emails,
  isLoading = false,
  error = null,
  onRetry,
  onReplySent,
  onEmailClick,
}) => {
  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <LoadingSpinner size="lg" />
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

  if (emails.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-slate-600">No emails found. Click "Fetch Emails" to load your inbox.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {emails.map((email) => (
        <EmailCard
          key={email.emailId}
          email={email}
          onReplySent={onReplySent}
          onEmailClick={onEmailClick}
        />
      ))}
    </div>
  );
};
