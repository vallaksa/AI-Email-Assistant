import React, { useState, useEffect } from 'react';
import type { EmailDto, ThreadMessageDto } from '../../types';
import { emailService } from '../../services/api/emailService';
import { Button } from '../common/Button';
import { EmailHeader } from './EmailHeader';
import { EmailBody } from './EmailBody';
import { EmailThread } from './EmailThread';
import { ReplySection } from '../reply/ReplySection';

export interface EmailDetailPanelProps {
  email: EmailDto | null;
  isOpen: boolean;
  onClose: () => void;
  onReplySent?: () => void;
}

export const EmailDetailPanel: React.FC<EmailDetailPanelProps> = ({
  email,
  isOpen,
  onClose,
  onReplySent,
}) => {
  const [threadMessages, setThreadMessages] = useState<ThreadMessageDto[]>([]);
  const [isLoadingThread, setIsLoadingThread] = useState(false);
  const [threadError, setThreadError] = useState<string | null>(null);
  const [isReplyExpanded, setIsReplyExpanded] = useState(false);

  useEffect(() => {
    if (isOpen && email?.emailId) {
      loadThread();
    } else {
      setThreadMessages([]);
      setThreadError(null);
      setIsReplyExpanded(false);
    }
  }, [isOpen, email?.emailId]);

  const loadThread = async () => {
    if (!email?.emailId) return;

    setIsLoadingThread(true);
    setThreadError(null);

    try {
      const messages = await emailService.getThread(email.emailId);
      setThreadMessages(messages);
    } catch (err) {
      setThreadError(err instanceof Error ? err.message : 'Failed to load thread');
    } finally {
      setIsLoadingThread(false);
    }
  };

  const handleReplySent = () => {
    setIsReplyExpanded(false);
    loadThread(); // Reload thread to show new reply
    onReplySent?.();
  };

  if (!isOpen || !email) {
    return null;
  }

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 z-40 transition-opacity"
        onClick={onClose}
      />

      {/* Side Panel */}
      <div
        className={`
          fixed top-0 right-0 h-full w-full max-w-2xl bg-white shadow-xl z-50
          transform transition-transform duration-300 ease-out
          ${isOpen ? 'translate-x-0' : 'translate-x-full'}
          overflow-y-auto
        `}
      >
        {/* Header */}
        <div className="sticky top-0 bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between z-10">
          <h2 className="text-xl font-semibold text-slate-900">Email Details</h2>
          <Button
            variant="text"
            size="sm"
            onClick={onClose}
            className="text-slate-600 hover:text-slate-800"
          >
            ✕ Close
          </Button>
        </div>

        {/* Content */}
        <div className="p-6">
          {email && (
            <>
              <EmailHeader
                from={email.from}
                subject={email.subject}
                receivedAt={email.receivedAt}
              />

              <div className="mt-4 mb-6">
                <EmailBody body={email.body || email.snippet} bodyType={email.bodyType} />
              </div>

              <div className="mb-6 pb-6 border-b border-slate-200">
                <Button
                  variant="primary"
                  onClick={() => setIsReplyExpanded(!isReplyExpanded)}
                  className="w-full"
                >
                  {isReplyExpanded ? 'Cancel Reply' : 'Reply'}
                </Button>
              </div>

              {isReplyExpanded && (
                <div className="mb-6">
                  <ReplySection
                    email={email}
                    onReplySent={handleReplySent}
                    onCancel={() => setIsReplyExpanded(false)}
                  />
                </div>
              )}

              <EmailThread
                messages={threadMessages}
                currentEmailId={email.emailId}
                isLoading={isLoadingThread}
                error={threadError}
                onRetry={loadThread}
              />
            </>
          )}
        </div>
      </div>
    </>
  );
};
