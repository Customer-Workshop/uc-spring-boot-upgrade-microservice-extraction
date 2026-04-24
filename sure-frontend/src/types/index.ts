export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  familyId: string;
  familyName: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface Account {
  id: string;
  familyId: string;
  name: string;
  accountType: string;
  subtype: string | null;
  currency: string;
  balance: number;
  status: string;
  active: boolean;
  institutionName: string | null;
  logoUrl: string | null;
  excludedFromTotals: boolean;
  createdAt: string;
}

export interface Entry {
  id: string;
  accountId: string;
  entryableType: string;
  name: string | null;
  date: string;
  amount: number;
  currency: string;
  notes: string | null;
  excluded: boolean;
  pending: boolean;
  markedAsTransfer: boolean;
  transaction: TransactionDetail | null;
  trade: TradeDetail | null;
  createdAt: string;
}

export interface TransactionDetail {
  id: string;
  categoryId: string | null;
  merchantId: string | null;
  kind: string;
  nature: string | null;
}

export interface TradeDetail {
  id: string;
  securityId: string | null;
  qty: number;
  price: number;
  tradeType: string;
}

export interface Category {
  id: string;
  familyId: string;
  name: string;
  color: string | null;
  icon: string | null;
  parentId: string | null;
  classification: string;
}

export interface Budget {
  id: string;
  familyId: string;
  categoryId: string | null;
  categoryName: string | null;
  name: string;
  amount: number;
  currency: string;
  periodType: string;
  startDate: string;
  endDate: string | null;
}

export interface Tag {
  id: string;
  familyId: string;
  name: string;
  color: string | null;
}

export interface Balance {
  id: string;
  accountId: string;
  date: string;
  balance: number;
  currency: string;
}

export interface Holding {
  id: string;
  accountId: string;
  securityId: string | null;
  securityTicker: string | null;
  securityName: string | null;
  date: string;
  qty: number;
  price: number;
  amount: number;
  currency: string;
  costBasis: number | null;
  costBasisSource: string | null;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
