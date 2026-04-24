import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAccounts } from '../api/accounts';
import { getEntries, createTransaction, deleteEntry } from '../api/transactions';
import { Plus, Trash2 } from 'lucide-react';

export default function TransactionsPage() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [selectedAccountId, setSelectedAccountId] = useState('');

  const { data: accounts } = useQuery({ queryKey: ['accounts'], queryFn: getAccounts });
  const accountIds = accounts?.map((a) => a.id) ?? [];

  const { data: entriesPage, isLoading } = useQuery({
    queryKey: ['entries', accountIds],
    queryFn: () => getEntries(accountIds),
    enabled: accountIds.length > 0,
  });

  const createMutation = useMutation({
    mutationFn: createTransaction,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['entries'] });
      setShowForm(false);
      setName('');
      setAmount('');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteEntry,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['entries'] }),
  });

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate({ accountId: selectedAccountId, name, date, amount: parseFloat(amount) });
  };

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);

  const getAccountName = (accountId: string) => accounts?.find((a) => a.id === accountId)?.name ?? 'Unknown';

  if (isLoading) return <div className="flex items-center justify-center h-64 text-gray-500">Loading...</div>;

  const entries = entriesPage?.content ?? [];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Transactions</h1>
        <button onClick={() => setShowForm(!showForm)}
          className="flex items-center gap-2 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium">
          <Plus size={16} /> Add Transaction
        </button>
      </div>
      {showForm && accounts && accounts.length > 0 && (
        <form onSubmit={handleCreate} className="bg-white rounded-xl border border-gray-200 p-6 mb-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Account</label>
              <select value={selectedAccountId} onChange={(e) => setSelectedAccountId(e.target.value)} required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none">
                <option value="">Select account</option>
                {accounts.map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
              <input value={name} onChange={(e) => setName(e.target.value)} required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Amount</label>
              <input type="number" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date</label>
              <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none" />
            </div>
          </div>
          <div className="flex gap-2">
            <button type="submit" disabled={createMutation.isPending}
              className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium disabled:opacity-50">
              {createMutation.isPending ? 'Creating...' : 'Create Transaction'}
            </button>
            <button type="button" onClick={() => setShowForm(false)}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 text-sm font-medium">Cancel</button>
          </div>
        </form>
      )}
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        {entries.length > 0 ? (
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Date</th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Name</th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Account</th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Type</th>
                <th className="text-right px-6 py-3 text-xs font-medium text-gray-500 uppercase">Amount</th>
                <th className="text-right px-6 py-3 text-xs font-medium text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {entries.map((entry) => (
                <tr key={entry.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm text-gray-600">{entry.date}</td>
                  <td className="px-6 py-4 font-medium text-gray-900">{entry.name ?? '—'}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{getAccountName(entry.accountId)}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                      entry.entryableType === 'Transaction' ? 'bg-blue-100 text-blue-700' :
                      entry.entryableType === 'Trade' ? 'bg-purple-100 text-purple-700' : 'bg-gray-100 text-gray-700'
                    }`}>{entry.entryableType}</span>
                  </td>
                  <td className="px-6 py-4 text-right font-medium">
                    <span className={entry.amount >= 0 ? 'text-green-600' : 'text-red-600'}>{formatCurrency(entry.amount)}</span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <button onClick={() => deleteMutation.mutate(entry.id)} className="text-gray-400 hover:text-red-600"><Trash2 size={16} /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="p-8 text-center text-gray-500">No transactions yet.</div>
        )}
      </div>
    </div>
  );
}
