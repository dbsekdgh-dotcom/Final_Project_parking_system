import kioskApi from "../../../shared/api/kioskApi";

export const requestPlateOcr = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const res = await kioskApi.post(
    `/api/kiosk/ocr/plate`,
    formData
  );

  return res.data;
};
