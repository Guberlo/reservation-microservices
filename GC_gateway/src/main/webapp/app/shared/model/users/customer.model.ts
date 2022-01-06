import { IUser } from 'app/shared/model/user.model';

export interface ICustomer {
  id?: number;
  name?: string;
  surname?: string;
  phone?: string;
  email?: string;
  gender?: string | null;
  user?: IUser | null;
}

export const defaultValue: Readonly<ICustomer> = {};
