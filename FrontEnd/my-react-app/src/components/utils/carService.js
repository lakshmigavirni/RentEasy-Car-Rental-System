import api from "./api";

export const getAllCars = () => {
  return api.get("/car-service/api/cars");
};
