import { IUser } from 'app/shared/model/user.model';

export interface IEmployee {
  id?: number;
  name?: string;
  surname?: string;
  phone?: string;
  user?: IUser | null;
}

export const defaultValue: Readonly<IEmployee> = {};
