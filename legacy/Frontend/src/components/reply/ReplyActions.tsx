import React from 'react';
import { Button } from '../common/Button';

export interface ReplyActionsProps {
  hasReply: boolean;
  isEditing: boolean;
  isGenerating: boolean;
  onGenerate: () => void;
  onRegenerate: () => void;
  onSaveEdit: () => void;
  onCancel: () => void;
}

export const ReplyActions: React.FC<ReplyActionsProps> = ({
  hasReply,
  isEditing,
  isGenerating,
  onGenerate,
  onRegenerate,
  onSaveEdit,
  onCancel,
}) => {
  return (
    <div className="flex flex-wrap gap-2">
      {!hasReply ? (
        <>
          <Button
            variant="primary"
            onClick={onGenerate}
            isLoading={isGenerating}
            disabled={isGenerating}
          >
            Generate Reply
          </Button>
          <Button
            variant="text"
            onClick={onCancel}
            disabled={isGenerating}
          >
            Cancel
          </Button>
        </>
      ) : (
        <>
          {isEditing ? (
            <>
              <Button
                variant="secondary"
                onClick={onSaveEdit}
              >
                Done Editing
              </Button>
              <Button
                variant="text"
                onClick={onCancel}
              >
                Cancel
              </Button>
            </>
          ) : (
            <>
              <Button
                variant="secondary"
                onClick={onRegenerate}
                disabled={isGenerating}
              >
                Regenerate
              </Button>
              <Button
                variant="text"
                onClick={onCancel}
              >
                Close
              </Button>
            </>
          )}
        </>
      )}
    </div>
  );
};
