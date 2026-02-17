import axios from 'axios';
import type { EmailDto, ReplyToEmailRequest, ReplyToEmailByIdRequest, ReplyResponse, ThreadMessageDto, ApiResponse } from '../../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add response interceptor to handle errors consistently
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Extract meaningful error message from response
    if (error.response) {
      // Server responded with error status
      const errorData = error.response.data;
      if (errorData && errorData.error && errorData.message) {
        // Use the error message from the API response
        throw new Error(errorData.message);
      } else if (errorData && errorData.message) {
        throw new Error(errorData.message);
      } else if (error.response.status === 503) {
        throw new Error('AI service is unavailable. Please check if the AI service (e.g., Ollama) is running.');
      } else {
        throw new Error(`Request failed with status code ${error.response.status}`);
      }
    } else if (error.request) {
      // Request was made but no response received
      throw new Error('Unable to connect to the server. Please check if the backend is running.');
    } else {
      // Something else happened
      throw new Error(error.message || 'An unexpected error occurred');
    }
  }
);

export const emailService = {
  /**
   * Fetch the most recent emails from Gmail inbox
   * @param limit Number of emails to fetch (10-50)
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
   * Fetch the most recent sent emails from Gmail
   * @param limit Number of emails to fetch (10-50)
   * @returns Promise with list of emails
   */
  async fetchSentEmails(limit: number): Promise<EmailDto[]> {
    const response = await apiClient.post<ApiResponse<EmailDto[]>>(
      `/api/emails/sent/${limit}`
    );
    
    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.error || 'Failed to fetch sent emails');
    }
    
    return response.data.data;
  },

  /**
   * Get a single email by its ID
   * @param emailId The unique identifier of the email
   * @returns Promise with email details
   */
  async getEmail(emailId: string): Promise<EmailDto> {
    const response = await apiClient.get<ApiResponse<EmailDto>>(
      `/api/emails/${emailId}`
    );
    
    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.error || 'Failed to fetch email');
    }
    
    return response.data.data;
  },

  /**
   * Get all messages in an email thread
   * @param emailId The unique identifier of the email
   * @returns Promise with list of thread messages
   */
  async getThread(emailId: string): Promise<ThreadMessageDto[]> {
    const response = await apiClient.get<ApiResponse<ThreadMessageDto[]>>(
      `/api/emails/${emailId}/thread`
    );
    
    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.error || 'Failed to fetch thread');
    }
    
    return response.data.data;
  },

  /**
   * Generate and send an AI-powered reply to a selected email (by index)
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

  /**
   * Generate and send an AI-powered reply to a selected email (by emailId)
   * @param request Reply request with emailId and optional instruction
   * @returns Promise with reply preview
   */
  async replyToEmailById(request: ReplyToEmailByIdRequest): Promise<ReplyResponse> {
    const response = await apiClient.post<ApiResponse<ReplyResponse>>(
      '/api/emails/reply/by-id',
      request
    );
    
    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.error || 'Failed to send reply');
    }
    
    return response.data.data;
  },
};
