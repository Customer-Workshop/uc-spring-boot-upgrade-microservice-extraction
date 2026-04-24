import apiClient from './client';
import type { Account, ApiResponse, Balance, Holding } from '../types';

export const getAccounts = async (): Promise<Account[]> => {
  const response = await apiClient.get<ApiResponse<Account[]>>('/accounts');
  return response.data.data;
};

export const getAccount = async (id: string): Promise<Account> => {
  const response = await apiClient.get<ApiResponse<Account>>(`/accounts/${id}`);
  return response.data.data;
};

export const createAccount = async (data: {
  name: string;
  accountType: string;
  subtype?: string;
  currency?: string;
  balance?: number;
  institutionName?: string;
}): Promise<Account> => {
  const response = await apiClient.post<ApiResponse<Account>>('/accounts', data);
  return response.data.data;
};

export const updateAccount = async (
  id: string,
  data: { name?: string; balance?: number; excludedFromTotals?: boolean }
): Promise<Account> => {
  const response = await apiClient.put<ApiResponse<Account>>(`/accounts/${id}`, data);
  return response.data.data;
};

export const deleteAccount = async (id: string): Promise<void> => {
  await apiClient.delete(`/accounts/${id}`);
};

export const getBalanceHistory = async (accountId: string, start: string, end: string): Promise<Balance[]> => {
  const response = await apiClient.get<ApiResponse<Balance[]>>(`/accounts/${accountId}/balances`, {
    params: { start, end },
  });
  return response.data.data;
};

export const getHoldings = async (accountId: string): Promise<Holding[]> => {
  const response = await apiClient.get<ApiResponse<Holding[]>>(`/accounts/${accountId}/holdings`);
  return response.data.data;
};
