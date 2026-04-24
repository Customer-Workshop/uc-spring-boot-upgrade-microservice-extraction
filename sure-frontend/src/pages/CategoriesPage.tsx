import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Trash2, Tag } from 'lucide-react';
import apiClient from '../api/client';
import type { ApiResponse, Category } from '../types';

const getCategories = async (): Promise<Category[]> => {
  const response = await apiClient.get<ApiResponse<Category[]>>('/categories');
  return response.data.data;
};

const createCategory = async (data: { name: string; classification: string; color?: string }): Promise<Category> => {
  const response = await apiClient.post<ApiResponse<Category>>('/categories', data);
  return response.data.data;
};

const deleteCategoryApi = async (id: string): Promise<void> => {
  await apiClient.delete(`/categories/${id}`);
};

export default function CategoriesPage() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [classification, setClassification] = useState('expense');

  const { data: categories, isLoading } = useQuery({ queryKey: ['categories'], queryFn: getCategories });

  const createMutation = useMutation({
    mutationFn: createCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] });
      setShowForm(false);
      setName('');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteCategoryApi,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['categories'] }),
  });

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate({ name, classification });
  };

  const incomeCategories = categories?.filter((c) => c.classification === 'income') ?? [];
  const expenseCategories = categories?.filter((c) => c.classification === 'expense') ?? [];

  if (isLoading) return <div className="flex items-center justify-center h-64 text-gray-500">Loading...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Categories</h1>
        <button onClick={() => setShowForm(!showForm)}
          className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium">
          <Plus size={16} /> Add Category
        </button>
      </div>
      {showForm && (
        <form onSubmit={handleCreate} className="bg-white rounded-xl border border-gray-200 p-6 mb-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
              <input value={name} onChange={(e) => setName(e.target.value)} required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
              <select value={classification} onChange={(e) => setClassification(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none">
                <option value="expense">Expense</option>
                <option value="income">Income</option>
              </select>
            </div>
          </div>
          <div className="flex gap-2">
            <button type="submit" disabled={createMutation.isPending}
              className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium disabled:opacity-50">
              {createMutation.isPending ? 'Creating...' : 'Create Category'}
            </button>
            <button type="button" onClick={() => setShowForm(false)}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 text-sm font-medium">Cancel</button>
          </div>
        </form>
      )}
      <div className="space-y-6">
        <div>
          <h2 className="text-lg font-semibold text-gray-900 mb-3">Income Categories</h2>
          <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
            {incomeCategories.length > 0 ? (
              <div className="divide-y divide-gray-100">
                {incomeCategories.map((cat) => (
                  <div key={cat.id} className="flex items-center justify-between px-6 py-3 hover:bg-gray-50">
                    <div className="flex items-center gap-3">
                      <Tag size={16} className="text-green-500" />
                      <span className="font-medium text-gray-900">{cat.name}</span>
                    </div>
                    <button onClick={() => deleteMutation.mutate(cat.id)} className="text-gray-400 hover:text-red-600"><Trash2 size={16} /></button>
                  </div>
                ))}
              </div>
            ) : <div className="p-6 text-center text-gray-500 text-sm">No income categories</div>}
          </div>
        </div>
        <div>
          <h2 className="text-lg font-semibold text-gray-900 mb-3">Expense Categories</h2>
          <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
            {expenseCategories.length > 0 ? (
              <div className="divide-y divide-gray-100">
                {expenseCategories.map((cat) => (
                  <div key={cat.id} className="flex items-center justify-between px-6 py-3 hover:bg-gray-50">
                    <div className="flex items-center gap-3">
                      <Tag size={16} className="text-red-500" />
                      <span className="font-medium text-gray-900">{cat.name}</span>
                    </div>
                    <button onClick={() => deleteMutation.mutate(cat.id)} className="text-gray-400 hover:text-red-600"><Trash2 size={16} /></button>
                  </div>
                ))}
              </div>
            ) : <div className="p-6 text-center text-gray-500 text-sm">No expense categories</div>}
          </div>
        </div>
      </div>
    </div>
  );
}
