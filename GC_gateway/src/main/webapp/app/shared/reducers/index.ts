import { loadingBarReducer as loadingBar } from 'react-redux-loading-bar';

import locale from './locale';
import authentication from './authentication';
import applicationProfile from './application-profile';

import administration from 'app/modules/administration/administration.reducer';
import userManagement from './user-management';
// prettier-ignore
import service from 'app/entities/reservation/service/service.reducer';
// prettier-ignore
import customer from 'app/entities/users/customer/customer.reducer';
// prettier-ignore
import reservation from 'app/entities/reservation/reservation/reservation.reducer';
// prettier-ignore
import employee from 'app/entities/users/employee/employee.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const rootReducer = {
  authentication,
  locale,
  applicationProfile,
  administration,
  userManagement,
  service,
  customer,
  reservation,
  employee,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
  loadingBar,
};

export default rootReducer;
