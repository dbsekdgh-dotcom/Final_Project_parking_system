 import { useState, useEffect, useCallback } from 'react';
  import {
      fetchActivePolicyApi, fetchPoliciesApi,
      createPolicyApi, updatePolicyApi
  } from '../../api/reservationPolicyApi';
  import Pagination from '../../../../shared/components/pagination/Pagination';
  import PolicyFormModal from '../components/PolicyFormModal';
  import './ReservationPolicyPage.css';

  const STATUS_LABEL = {
      ACTIVE:    '활성',
      SCHEDULED: '예정',
      EXPIRED:   '만료',
      PERMANENT: '무기한',
  };

  const EMPTY_FORM = {
      eventName:                '',
      startDate:                '',
      dailyLimitPerHousehold:   '',
      monthlyLimitPerHousehold: '',
      maxActiveReservations:    1,
      permittedMinutes:         60,
      noShowPenaltyEnabled:     false,
  };

  export default function ReservationPolicyPage() {
      const [activePolicy, setActivePolicy] = useState(null);
      const [policies, setPolicies]         = useState([]);
      const [page, setPage]                 = useState(0);
      const [totalPages, setTotalPages]     = useState(0);
      const [modalOpen, setModalOpen]       = useState(false);
      const [editTarget, setEditTarget]     = useState(null);
      const [form, setForm]                 = useState(EMPTY_FORM);
      const [submitting, setSubmitting]     = useState(false);

      const fetchActive = useCallback(() => {
          fetchActivePolicyApi()
              .then(res => setActivePolicy(res.data))
              .catch(() => setActivePolicy(null));
      }, []);

      const fetchPolicies = useCallback(() => {
          fetchPoliciesApi(page)
              .then(res => {
                  setPolicies(res.data.content ?? []);
                  setTotalPages(res.data.totalPages);
              })
              .catch(err => console.error(err));
      }, [page]);

      useEffect(() => {
          fetchActive();
          fetchPolicies();
      }, [fetchActive, fetchPolicies]);

      const openCreate = () => {
          setEditTarget(null);
          setForm(EMPTY_FORM);
          setModalOpen(true);
      };

      const openEdit = (policy) => {
          setEditTarget(policy);
          setForm({
              eventName:                policy.eventName,
              startDate:                policy.startDate?.slice(0, 16) ?? '',
              dailyLimitPerHousehold:   policy.dailyLimitPerHousehold ?? '',
              monthlyLimitPerHousehold: policy.monthlyLimitPerHousehold ?? '',
              maxActiveReservations:    policy.maxActiveReservations,
              permittedMinutes:         policy.permittedMinutes,
              noShowPenaltyEnabled:     policy.noShowPenaltyEnabled,
          });
          setModalOpen(true);
      };

      const handleSubmit = async () => {
          setSubmitting(true);
          try {
              const dto = {
                  eventName:                form.eventName,
                  startDate:                form.startDate || null,
                  dailyLimitPerHousehold:   form.dailyLimitPerHousehold  !== '' ? Number(form.dailyLimitPerHousehold)  : null,
                  monthlyLimitPerHousehold: form.monthlyLimitPerHousehold !== '' ? Number(form.monthlyLimitPerHousehold) : null,
                  maxActiveReservations:    Number(form.maxActiveReservations),
                  permittedMinutes:         Number(form.permittedMinutes),
                  noShowPenaltyEnabled:     form.noShowPenaltyEnabled,
              };
              if (editTarget) {
                  await updatePolicyApi(editTarget.id, dto);
              } else {
                  await createPolicyApi(dto);
              }
              setModalOpen(false);
              fetchActive();
              fetchPolicies();
          } catch (err) {
              console.error(err);
          } finally {
              setSubmitting(false);
          }
      };

      return (
          <div className="rpp">
              {/* 현재 활성 정책 카드 */}
              <div className="rpp__active-section">
                  <span className="rpp__section-title">현재 활성 정책</span>
                  {activePolicy ? (
                      <div className="rpp__active-card">
                          <div className="rpp__active-name">{activePolicy.eventName}</div>
                          <div className="rpp__active-meta">
                              <span>{activePolicy.startDate?.slice(0, 10)} ~ {activePolicy.endDate?.slice(0, 10) ?? '무기한'}</span>
                              <span>일일 {activePolicy.dailyLimitPerHousehold ?? '무제한'}회</span>
                              <span>월 {activePolicy.monthlyLimitPerHousehold ?? '무제한'}회</span>
                              <span>동시 {activePolicy.maxActiveReservations}건</span>
                              <span>허용 {activePolicy.permittedMinutes}분</span>
                              <span>노쇼 페널티 {activePolicy.noShowPenaltyEnabled ? 'ON' : 'OFF'}</span>
                          </div>
                      </div>
                  ) : (
                      <div className="rpp__active-empty">현재 활성 정책이 없습니다.</div>
                  )}
              </div>

              {/* 목록 헤더 */}
              <div className="rpp__list-header">
                  <span className="rpp__section-title">전체 정책 목록</span>
                  <button className="rpp__create-btn" onClick={openCreate}>+ 정책 생성</button>
              </div>

              {/* 정책 목록 테이블 */}
              <div className="rpp__table-wrap">
                  <table className="rpp__table">
                      <thead>
                          <tr>
                              <th>ID</th>
                              <th>정책명</th>
                              <th>시작일</th>
                              <th>종료일</th>
                              <th>일일 한도</th>
                              <th>월간 한도</th>
                              <th>동시 예약</th>
                              <th>허용(분)</th>
                              <th>노쇼</th>
                              <th>상태</th>
                              <th>등록자</th>
                              <th>수정</th>
                          </tr>
                      </thead>
                      <tbody>
                          {policies.length === 0 ? (
                              <tr>
                                  <td colSpan={12} className="rpp__empty">등록된 정책이 없습니다.</td>
                              </tr>
                          ) : policies.map(p => (
                              <tr key={p.id}>
                                  <td>{p.id}</td>
                                  <td>{p.eventName}</td>
                                  <td>{p.startDate?.slice(0, 10)}</td>
                                  <td>{p.endDate?.slice(0, 10) ?? '무기한'}</td>
                                  <td>{p.dailyLimitPerHousehold ?? '무제한'}</td>
                                  <td>{p.monthlyLimitPerHousehold ?? '무제한'}</td>
                                  <td>{p.maxActiveReservations}</td>
                                  <td>{p.permittedMinutes}</td>
                                  <td>{p.noShowPenaltyEnabled ? 'ON' : 'OFF'}</td>
                                  <td>
                                      <span className={`rpp__badge rpp__badge--${p.policyStatus?.toLowerCase()}`}>
                                          {STATUS_LABEL[p.policyStatus] ?? p.policyStatus}
                                      </span>
                                  </td>
                                  <td>{p.adminLoginId}</td>
                                   <td>                                                                                                                                                           
                                    {p.policyStatus === 'ACTIVE' ? (
                                        <span className="rpp__btn rpp__btn--disabled">활성중</span>
                                    ) : (
                                        <button className="rpp__btn rpp__btn--edit" onClick={() => openEdit(p)}>
                                            수정
                                        </button>
                                    )}
                                </td>
                              </tr>
                          ))}
                      </tbody>
                  </table>
              </div>

              <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

              {modalOpen && (
                  <PolicyFormModal
                      editTarget={editTarget}
                      form={form}
                      onFormChange={setForm}
                      onSubmit={handleSubmit}
                      onClose={() => setModalOpen(false)}
                      submitting={submitting}
                  />
              )}
          </div>
      );
  }