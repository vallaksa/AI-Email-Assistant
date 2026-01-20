// Types matching backend DTOs

export interface EmailDto {
  index: number;
  emailId: string;
  from: string;
  subject: string;
  snippet: string;
  receivedAt: string; // ISO 8601 format
}

export interface ReplyToEmailRequest {
  index: number;
  userInstruction?: string;
}

export interface ReplyResponse {
  index: number;
  replyPreview: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
}
