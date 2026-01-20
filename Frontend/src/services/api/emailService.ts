import axios from 'axios';
import type { EmailDto, ReplyToEmailRequest, ReplyResponse, ApiResponse } from '../../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const emailService = {
  /**
   * Fetch the most recent emails from Gmail inbox
   * @param limit Number of emails to fetch (15-50)
   * @returns Promise with list of emails
   */
  async fetchEmails(limit: number): Promise<EmailDto[]> {
    const response = await apiClient.post<ApiResponse<EmailDto[]>>(
      `/api/emails/fetch/${limit}`
    );
    
    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.error || 'Failed to fetch emails');
    }
    
    return response.data.data;
  },

  /**
   * Generate and send an AI-powered reply to a selected email
   * @param request Reply request with index and optional instruction
   * @returns Promise with reply preview
   */
  async replyToEmail(request: ReplyToEmailRequest): Promise<ReplyResponse> {
    const response = await apiClient.post<ApiResponse<ReplyResponse>>(
      '/api/emails/reply',
      request
    );
    
    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.error || 'Failed to send reply');
    }
    
    return response.data.data;
  },
};
