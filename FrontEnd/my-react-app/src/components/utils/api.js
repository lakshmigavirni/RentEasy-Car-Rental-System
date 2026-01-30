import axios from "axios";
import url from "../URL";

const api = axios.create({
  baseURL: url,
});

export default api;
