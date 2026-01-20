import React from 'react';
import { Textarea } from '../common/Textarea';

export interface InstructionInputProps {
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export const InstructionInput: React.FC<InstructionInputProps> = ({
  value,
  onChange,
  disabled = false,
}) => {
  return (
    <div className="mb-4">
      <Textarea
        label="Instruction (optional)"
        placeholder="Write a polite follow-up confirming next steps..."
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={disabled}
        className="min-h-20"
      />
    </div>
  );
};
