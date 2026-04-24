import apiClient from './client';
import type { ApiResponse, AuthResponse } from '../types';

export const login = async (email: string, password: string): Promise<AuthResponse> => {
  const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', { email, password });
  return response.data.data;
};

export const register = async (data: {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  familyName?: string;
  currency?: string;
}): Promise<AuthResponse> => {
  const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', data);
  return response.data.data;
};

export const getCurrentUser = async (): Promise<AuthResponse> => {
  const response = await apiClient.get<ApiResponse<AuthResponse>>('/auth/me');
  return response.data.data;
};
