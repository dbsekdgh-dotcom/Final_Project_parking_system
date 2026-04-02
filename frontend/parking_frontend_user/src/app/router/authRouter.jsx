import LoginPage from "../../features/auth/pages/LoginPage"
import SignupPage from "../../features/auth/pages/SignupPage"

const authRouter = [
    {
        path: "login",
        element: <LoginPage />,
    },
    {
        path: "signup",
        element: <SignupPage />,
    },
];

export default authRouter;