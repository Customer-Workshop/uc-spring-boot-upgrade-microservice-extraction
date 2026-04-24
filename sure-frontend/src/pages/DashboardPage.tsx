import { useQuery } from '@tanstack/react-query';
import { getAccounts } from '../api/accounts';
import { Wallet, TrendingUp, TrendingDown, DollarSign } from 'lucide-react';

export default function DashboardPage() {
  const { data: accounts, isLoading } = useQuery({
    queryKey: ['accounts'],
    queryFn: getAccounts,
  });

  const assets = accounts?.filter((a) => ['depository', 'investment', 'crypto', 'property', 'vehicle', 'other_asset'].includes(a.accountType)) ?? [];
  const liabilities = accounts?.filter((a) => ['credit_card', 'loan', 'other_liability'].includes(a.accountType)) ?? [];

  const totalAssets = assets.reduce((sum, a) => sum + a.balance, 0);
  const totalLiabilities = liabilities.reduce((sum, a) => sum + Math.abs(a.balance), 0);
  const netWorth = totalAssets - totalLiabilities;

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);

  if (isLoading) {
    return <div className="flex items-center justify-center h-64 text-gray-500">Loading...</div>;
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-green-100 rounded-lg"><TrendingUp className="text-green-600" size={20} /></div>
            <span className="text-sm font-medium text-gray-500">Total Assets</span>
          </div>
          <p className="text-2xl font-bold text-gray-900">{formatCurrency(totalAssets)}</p>
          <p className="text-sm text-gray-500 mt-1">{assets.length} accounts</p>
        </div>
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-red-100 rounded-lg"><TrendingDown className="text-red-600" size={20} /></div>
            <span className="text-sm font-medium text-gray-500">Total Liabilities</span>
          </div>
          <p className="text-2xl font-bold text-gray-900">{formatCurrency(totalLiabilities)}</p>
          <p className="text-sm text-gray-500 mt-1">{liabilities.length} accounts</p>
        </div>
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-primary-100 rounded-lg"><DollarSign className="text-primary-600" size={20} /></div>
            <span className="text-sm font-medium text-gray-500">Net Worth</span>
          </div>
          <p className={`text-2xl font-bold ${netWorth >= 0 ? 'text-green-600' : 'text-red-600'}`}>
            {formatCurrency(netWorth)}
          </p>
        </div>
      </div>
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Accounts</h2>
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        {accounts && accounts.length > 0 ? (
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Name</th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Type</th>
                <th className="text-left px-6 py-3 text-xs font-medium text-gray-500 uppercase">Institution</th>
                <th className="text-right px-6 py-3 text-xs font-medium text-gray-500 uppercase">Balance</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {accounts.map((account) => (
                <tr key={account.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <Wallet size={16} className="text-gray-400" />
                      <span className="font-medium text-gray-900">{account.name}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600 capitalize">{account.accountType.replace('_', ' ')}</td>
                  <td className="px-6 py-4 text-sm text-gray-600">{account.institutionName ?? '—'}</td>
                  <td className="px-6 py-4 text-right font-medium">
                    <span className={account.balance >= 0 ? 'text-green-600' : 'text-red-600'}>
                      {formatCurrency(account.balance)}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="p-8 text-center text-gray-500">No accounts yet. Create your first account to get started.</div>
        )}
      </div>
    </div>
  );
}
