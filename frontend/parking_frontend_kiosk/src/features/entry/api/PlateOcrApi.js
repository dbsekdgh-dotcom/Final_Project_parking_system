import axios from "axios";

export const requestPlateOcr = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  const res = await axios.post(
    "/api/v1/parking/entryexit/",
    formData
  );

  return res.data;
};
