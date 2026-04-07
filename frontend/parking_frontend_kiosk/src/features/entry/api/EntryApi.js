import axios from "axios";

export const createEntry = async ({ file }) => {
  const formData = new FormData();
  formData.append("file", file);

  const res = await axios.post(
    "http://localhost:8081/api/v1/entry",
    formData
  );

  return res.data;
};

