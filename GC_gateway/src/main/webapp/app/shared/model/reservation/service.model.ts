import { IReservation } from 'app/shared/model/reservation/reservation.model';

export interface IService {
  id?: number;
  name?: string;
  description?: string;
  duration?: number;
  price?: number;
  reservations?: IReservation[] | null;
}

export const defaultValue: Readonly<IService> = {};
