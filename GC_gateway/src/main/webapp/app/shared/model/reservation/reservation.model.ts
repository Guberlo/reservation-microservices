import dayjs from 'dayjs';
import { IService } from 'app/shared/model/reservation/service.model';

export interface IReservation {
  id?: number;
  date?: string;
  startTime?: string;
  endTime?: string;
  services?: IService[] | null;
}

export const defaultValue: Readonly<IReservation> = {};
