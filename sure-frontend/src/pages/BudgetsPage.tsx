import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PieChart } from 'lucide-react';
import apiClient from '../api/client';
import type { ApiResponse, Budget } from '../types';

const getBudgets = async (): Promise<Budget[]> => {
  const response = await apiClient.get<ApiResponse<Budget[]>>('/budgets');
  return response.data.data;
};

export default function BudgetsPage() {
  const { data: budgets, isLoading } = useQuery({ queryKey: ['budgets'], queryFn: getBudgets });

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);

  if (isLoading) return <div className="flex items-center justify-center h-64 text-gray-500">Loading...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Budgets</h1>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {budgets && budgets.length > 0 ? (
          budgets.map((budget) => (
            <div key={budget.id} className="bg-white rounded-xl border border-gray-200 p-6">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 bg-primary-100 rounded-lg"><PieChart className="text-primary-600" size={20} /></div>
                <div>
                  <h3 className="font-semibold text-gray-900">{budget.name}</h3>
                  {budget.categoryName && <p className="text-sm text-gray-500">{budget.categoryName}</p>}
                </div>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-500 capitalize">{budget.periodType}</span>
                <span className="text-lg font-bold text-primary-600">{formatCurrency(budget.amount)}</span>
              </div>
              <div className="mt-3 bg-gray-100 rounded-full h-2">
                <div className="bg-primary-500 h-2 rounded-full" style={{ width: '0%' }} />
              </div>
              <p className="text-xs text-gray-500 mt-1">{formatCurrency(0)} of {formatCurrency(budget.amount)} used</p>
            </div>
          ))
        ) : (
          <div className="col-span-full bg-white rounded-xl border border-gray-200 p-8 text-center text-gray-500">
            No budgets yet. Create your first budget to start tracking spending.
          </div>
        )}
      </div>
    </div>
  );
}
