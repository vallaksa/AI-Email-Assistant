import React from 'react';
import { Textarea } from '../common/Textarea';

export interface ReplyPreviewProps {
  value: string;
  onChange: (value: string) => void;
  isEditing: boolean;
  onEditToggle: () => void;
}

export const ReplyPreview: React.FC<ReplyPreviewProps> = ({
  value,
  onChange,
  isEditing,
  onEditToggle,
}) => {
  if (!value) {
    return null;
  }

  return (
    <div className="mb-4">
      <div className="flex items-center justify-between mb-2">
        <label className="block text-sm font-medium text-slate-700">
          Generated Reply ✨
        </label>
        {!isEditing && (
          <button
            onClick={onEditToggle}
            className="text-sm text-indigo-600 hover:text-indigo-700 font-medium"
          >
            Edit
          </button>
        )}
      </div>
      <Textarea
        value={value}
        onChange={(e) => onChange(e.target.value)}
        readOnly={!isEditing}
        className={`min-h-32 ${isEditing ? 'bg-white border-indigo-500 focus:ring-indigo-500' : 'bg-slate-50 border-slate-300'}`}
      />
    </div>
  );
};
