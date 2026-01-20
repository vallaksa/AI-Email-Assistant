import React from 'react';

export interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
  className?: string;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({
  message,
  onRetry,
  className = '',
}) => {
  return (
    <div className={`bg-rose-50 border border-rose-200 rounded-md p-3 ${className}`}>
      <div className="flex items-start gap-2">
        <svg
          className="h-5 w-5 text-rose-600 flex-shrink-0 mt-0.5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <div className="flex-1">
          <p className="text-sm text-rose-600">{message}</p>
          {onRetry && (
            <button
              onClick={onRetry}
              className="mt-2 text-sm font-medium text-rose-700 hover:text-rose-800 underline"
            >
              Retry
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
