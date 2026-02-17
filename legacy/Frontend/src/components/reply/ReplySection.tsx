import React, { useState } from 'react';
import type { EmailDto } from '../../types';
import { emailService } from '../../services/api/emailService';
import { InstructionInput } from './InstructionInput';
import { ReplyPreview } from './ReplyPreview';
import { ReplyActions } from './ReplyActions';
import { ErrorMessage } from '../common/ErrorMessage';
import { LoadingSpinner } from '../common/LoadingSpinner';

export interface ReplySectionProps {
  email: EmailDto;
  onReplySent?: () => void;
  onCancel: () => void;
}

type ReplyState = 'empty' | 'generating' | 'generated' | 'editing' | 'error';

export const ReplySection: React.FC<ReplySectionProps> = ({
  email,
  onReplySent,
  onCancel,
}) => {
  const [state, setState] = useState<ReplyState>('empty');
  const [instruction, setInstruction] = useState('');
  const [reply, setReply] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);

  const handleGenerate = async () => {
    setState('generating');
    setError(null);
    
    try {
      // Use emailId if index is not available (e.g., from detail panel)
      const response = email.index > 0
        ? await emailService.replyToEmail({
            index: email.index,
            userInstruction: instruction || undefined,
          })
        : await emailService.replyToEmailById({
            emailId: email.emailId,
            userInstruction: instruction || undefined,
          });
      
      // Note: Reply is already sent by the backend when this API is called
      setReply(response.replyPreview);
      setState('generated');
      setIsEditing(false);
      onReplySent?.();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to generate reply');
      setState('error');
    }
  };

  const handleRegenerate = async () => {
    setState('generating');
    setError(null);
    
    try {
      // Use emailId if index is not available (e.g., from detail panel)
      const response = email.index > 0
        ? await emailService.replyToEmail({
            index: email.index,
            userInstruction: instruction || undefined,
          })
        : await emailService.replyToEmailById({
            emailId: email.emailId,
            userInstruction: instruction || undefined,
          });
      
      // Note: Reply is already sent by the backend when this API is called
      setReply(response.replyPreview);
      setState('generated');
      setIsEditing(false);
      onReplySent?.();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to regenerate reply');
      setState('error');
    }
  };

  const handleEditToggle = () => {
    setIsEditing(!isEditing);
    setState(isEditing ? 'generated' : 'editing');
  };

  const handleSaveEdit = () => {
    setIsEditing(false);
    setState('generated');
  };


  const handleCancel = () => {
    setState('empty');
    setReply('');
    setInstruction('');
    setError(null);
    setIsEditing(false);
    onCancel();
  };

  return (
    <div className="bg-indigo-50 border-l-4 border-indigo-500 rounded-r-lg p-4 transition-all duration-300 ease-out">
      <h3 className="text-lg font-semibold text-slate-900 mb-4">Reply Assistant</h3>

      {error && (
        <ErrorMessage
          message={error}
          onRetry={state === 'error' ? handleGenerate : undefined}
          className="mb-4"
        />
      )}

      <InstructionInput
        value={instruction}
        onChange={setInstruction}
        disabled={state === 'generating'}
      />

      {state === 'generating' && (
        <div className="flex items-center gap-2 mb-4 text-slate-600">
          <LoadingSpinner size="sm" />
          <span className="text-sm">Generating reply...</span>
        </div>
      )}

      {reply && (
        <>
          <ReplyPreview
            value={reply}
            onChange={setReply}
            isEditing={isEditing}
            onEditToggle={handleEditToggle}
          />
          {!isEditing && (
            <div className="mb-4 p-3 bg-emerald-50 border border-emerald-200 rounded-md">
              <p className="text-sm text-emerald-700">
                ✓ Reply has been sent. You can edit the preview above for reference, but it won't be re-sent.
              </p>
            </div>
          )}
        </>
      )}

        <ReplyActions
          hasReply={!!reply}
          isEditing={isEditing}
          isGenerating={state === 'generating'}
          onGenerate={handleGenerate}
          onRegenerate={handleRegenerate}
          onSaveEdit={handleSaveEdit}
          onCancel={handleCancel}
        />
    </div>
  );
};
