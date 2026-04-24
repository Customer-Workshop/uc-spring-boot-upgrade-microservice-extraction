import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAccounts, createAccount, deleteAccount } from '../api/accounts';
import { Plus, Trash2, Wallet } from 'lucide-react';

const accountTypes = [
  { value: 'depository', label: 'Depository (Checking/Savings)' },
  { value: 'investment', label: 'Investment' },
  { value: 'credit_card', label: 'Credit Card' },
  { value: 'loan', label: 'Loan' },
  { value: 'property', label: 'Property' },
  { value: 'vehicle', label: 'Vehicle' },
  { value: 'crypto', label: 'Crypto' },
  { value: 'other_asset', label: 'Other Asset' },
  { value: 'other_liability', label: 'Other Liability' },
];

export default function AccountsPage() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [accountType, setAccountType] = useState('depository');
  const [balance, setBalance] = useState('0');
  const [institutionName, setInstitutionName] = useState('');

  const { data: accounts, isLoading } = useQuery({ queryKey: ['accounts'], queryFn: getAccounts });

  const createMutation = useMutation({
    mutationFn: createAccount,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      setShowForm(false);
      setName('');
      setBalance('0');
      setInstitutionName('');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteAccount,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['accounts'] }),
  });

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate({ name, accountType, balance: parseFloat(balance), institutionName: institutionName || undefined });
  };

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);

  if (isLoading) return <div className="flex items-center justify-center h-64 text-gray-500">Loading...</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Accounts</h1>
        <button onClick={() => setShowForm(!showForm)}
          className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium">
          <Plus size={16} /> Add Account
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
              <select value={accountType} onChange={(e) => setAccountType(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none">
                {accountTypes.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Balance</label>
              <input type="number" step="0.01" value={balance} onChange={(e) => setBalance(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Institution</label>
              <input value={institutionName} onChange={(e) => setInstitutionName(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none" />
            </div>
          </div>
          <div className="flex gap-2">
            <button type="submit" disabled={createMutation.isPending}
              className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium disabled:opacity-50">
              {createMutation.isPending ? 'Creating...' : 'Create Account'}
            </button>
            <button type="button" onClick={() => setShowForm(false)}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 text-sm font-medium">Cancel</button>
          </div>
        </form>
      )}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        {accounts && accounts.length > 0 ? (
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Name</th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Type</th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Institution</th>
                <th className="text-right px-6 py-3 text-xs font-medium text-gray-500 uppercase">Balance</th>
                <th className="text-right px-6 py-3 text-xs font-medium text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {accounts.map((account) => (
                <tr key={account.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4"><div className="flex items-center gap-3"><Wallet size={16} className="text-gray-400" /><span className="font-medium text-gray-900">{account.name}</span></div></td>
                  <td className="px-6 py-4 text-sm text-gray-600 capitalize">{account.accountType.replace('_', ' ')}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{account.institutionName ?? '—'}</td>
                  <td className="px-6 py-4 text-right font-medium"><span className={account.balance >= 0 ? 'text-green-600' : 'text-red-600'}>{formatCurrency(account.balance)}</span></td>
                  <td className="px-6 py-4 text-right">
                    <button onClick={() => deleteMutation.mutate(account.id)} className="text-gray-400 hover:text-red-600"><Trash2 size={16} /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="p-8 text-center text-gray-500">No accounts yet.</div>
        )}
      </div>
    </div>
  );
}
