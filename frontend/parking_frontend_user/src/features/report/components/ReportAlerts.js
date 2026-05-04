import Swal from 'sweetalert2';

const BASE = {
  confirmButtonColor: '#32324d',
  cancelButtonColor: '#9e9eb8',
  didOpen: () => {
    const container = document.querySelector('.swal2-container');
    if (container) container.style.zIndex = '99999';
  },
};

export const confirmCancelReport = () =>
  Swal.fire({
    title: '신고를 취소하시겠습니까?',
    text: '취소 후에는 되돌릴 수 없습니다.',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#c62828',
    cancelButtonColor: BASE.cancelButtonColor,
    confirmButtonText: '취소하기',
    cancelButtonText: '닫기',
  });

export const alertReportSuccess = () =>
  Swal.fire({
    icon: 'success',
    title: '신고 접수 완료',
    text: '신고가 정상적으로 접수되었습니다.',
    ...BASE,
    confirmButtonText: '확인',
  });

export const alertReportError = (message) =>
  Swal.fire({
    icon: 'error',
    title: '접수 실패',
    text: message || '신고 접수에 실패했습니다. 다시 시도해 주세요.',
    ...BASE,
    confirmButtonText: '확인',
  });

export const alertValidation = (message) =>
  Swal.fire({
    icon: 'warning',
    title: '입력 확인',
    text: message,
    ...BASE,
    confirmButtonText: '확인',
  });
