import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntity } from './service.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const ServiceDetail = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getEntity(props.match.params.id));
  }, []);

  const serviceEntity = useAppSelector(state => state.service.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="serviceDetailsHeading">
          <Translate contentKey="gcGatewayApp.reservationService.detail.title">Service</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{serviceEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="gcGatewayApp.reservationService.name">Name</Translate>
            </span>
          </dt>
          <dd>{serviceEntity.name}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="gcGatewayApp.reservationService.description">Description</Translate>
            </span>
          </dt>
          <dd>{serviceEntity.description}</dd>
          <dt>
            <span id="duration">
              <Translate contentKey="gcGatewayApp.reservationService.duration">Duration</Translate>
            </span>
          </dt>
          <dd>{serviceEntity.duration}</dd>
          <dt>
            <span id="price">
              <Translate contentKey="gcGatewayApp.reservationService.price">Price</Translate>
            </span>
          </dt>
          <dd>{serviceEntity.price}</dd>
        </dl>
        <Button tag={Link} to="/service" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/service/${serviceEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ServiceDetail;
