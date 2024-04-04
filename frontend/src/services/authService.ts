import axios from "axios";
import {ILogin, IRegistration, IUser, IUserResponse} from "../types/types";
import authHeader from "./auth-header";
import {Dispatch} from "redux";
import {login, setUserData} from "../slices/authSlice";
import {AppDispatch} from "../store";

const API_URL = "/api/auth";

/**
 * Запрос для регистрации пользователя
 * @constructor
 */
const register = (registration: IRegistration) => {
    const { username, number, dateOfBirth, email, password } = registration;
    return axios.post(API_URL + "/signup", {
        username,
        number,
        dateOfBirth,
        email,
        password,
    });
};

/**
 * Запрос для аутентификации пользователя
 * @constructor
 */
const loginUser = async (loginData: ILogin, dispatch: Dispatch) => {
    const { username, password } = loginData;

    let response = await axios.post(API_URL + "/signin", {
            username,
            password,
        })
    console.log(response);
    dispatch(login(response.data));

    const headers = authHeader();
    console.log(headers);
    let detailsResponse = await axios
        .get<IUser>(API_URL, {headers});
    console.log(detailsResponse);
    dispatch(setUserData(detailsResponse.data));

    return detailsResponse.data;

};
const refresh = async (refresh_token: String, dispatch: Dispatch): Promise<IUser> => {
    let response = await axios
        .post<IUser>(API_URL + "/refresh", {
            refresh_token
        });
    console.log(response)
    dispatch(login(response.data));

    const headers = authHeader();

    let detailsResponse = await axios
        .get<IUser>("api/auth", { headers });
    console.log(response)
    dispatch(setUserData(detailsResponse.data));

    return detailsResponse.data;
};

const updateUser = async (user: IUserResponse, dispatch: AppDispatch) => {
    const headers = authHeader();
    let response = await axios
        .put<IUser>("api/auth", user, {headers});
    console.log(response);
    console.log(`Обновление данных пользователя ${API_URL}}`, user, {headers: authHeader()})

    let detailsResponse = await axios
        .get<IUser>("api/auth", { headers });
    dispatch(setUserData(detailsResponse.data));
    return detailsResponse;
};

/**
 * Выход из аккаунта пользователя
 * @constructor
 */
const logout = () => {
    console.log("logout");
    localStorage.removeItem("user");
};

const authService = {
    register,
    loginUser,
    logout,
    updateUser,
    refresh
};

export default authService;