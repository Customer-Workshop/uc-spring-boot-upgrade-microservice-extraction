import apiClient from './client';
import type { ApiResponse, Entry, PageResponse } from '../types';

export const getEntries = async (
  accountIds: string[],
  page = 0,
  size = 25
): Promise<PageResponse<Entry>> => {
  const response = await apiClient.get<ApiResponse<PageResponse<Entry>>>('/entries', {
    params: { accountIds: accountIds.join(','), page, size },
  });
  return response.data.data;
};

export const getEntry = async (id: string): Promise<Entry> => {
  const response = await apiClient.get<ApiResponse<Entry>>(`/entries/${id}`);
  return response.data.data;
};

export const createTransaction = async (data: {
  accountId: string;
  name: string;
  date: string;
  amount: number;
  currency?: string;
  notes?: string;
  categoryId?: string;
  merchantId?: string;
}): Promise<Entry> => {
  const response = await apiClient.post<ApiResponse<Entry>>('/transactions', data);
  return response.data.data;
};

export const updateTransaction = async (
  id: string,
  data: {
    name?: string;
    date?: string;
    amount?: number;
    notes?: string;
    categoryId?: string;
    excluded?: boolean;
  }
): Promise<Entry> => {
  const response = await apiClient.put<ApiResponse<Entry>>(`/entries/${id}`, data);
  return response.data.data;
};

export const deleteEntry = async (id: string): Promise<void> => {
  await apiClient.delete(`/entries/${id}`);
};
