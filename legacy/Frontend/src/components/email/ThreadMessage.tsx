import React from 'react';
import type { ThreadMessageDto } from '../../types';
import { formatDate } from '../../utils/dateFormatter';
import { EmailBody } from './EmailBody';

export interface ThreadMessageProps {
  message: ThreadMessageDto;
  isCurrentEmail?: boolean;
}

export const ThreadMessage: React.FC<ThreadMessageProps> = ({
  message,
  isCurrentEmail = false,
}) => {
  return (
    <div
      className={`
        p-4 rounded-lg mb-4 border transition-colors
        ${
          isCurrentEmail
            ? 'bg-indigo-50 border-indigo-200'
            : message.isSent
            ? 'bg-slate-50 border-slate-200'
            : 'bg-white border-slate-200'
        }
      `}
    >
      <div className="flex items-start justify-between mb-2">
        <div className="flex-1">
          <p className={`text-sm font-medium ${message.isSent ? 'text-indigo-600' : 'text-slate-900'}`}>
            {message.isSent ? 'You' : message.from}
          </p>
          <p className="text-xs text-slate-500 mt-1">
            {formatDate(message.receivedAt)}
          </p>
        </div>
        {isCurrentEmail && (
          <span className="px-2 py-1 text-xs font-medium bg-indigo-100 text-indigo-700 rounded">
            Current
          </span>
        )}
      </div>
      
      <div className="mt-3">
        <EmailBody body={message.body} bodyType={message.bodyType} />
      </div>
    </div>
  );
};
