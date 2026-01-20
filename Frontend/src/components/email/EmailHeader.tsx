import React from 'react';
import { formatDate } from '../../utils/dateFormatter';

export interface EmailHeaderProps {
  from: string;
  subject: string;
  receivedAt: string;
}

export const EmailHeader: React.FC<EmailHeaderProps> = ({
  from,
  subject,
  receivedAt,
}) => {
  return (
    <div className="mb-3">
      <div className="flex items-start justify-between gap-4 mb-1">
        <p className="text-sm font-medium text-slate-900 flex-1">
          {from}
        </p>
        <p className="text-xs text-slate-500 whitespace-nowrap">
          {formatDate(receivedAt)}
        </p>
      </div>
      <p className="text-base font-semibold text-slate-800">
        {subject}
      </p>
    </div>
  );
};
