import { RouterProvider } from 'react-router-dom';
import { router } from '@/router';
import LoginModal from '@/components/auth/LoginModal';

function App() {
  return (
    <>
      <RouterProvider router={router} />
      <LoginModal />
    </>
  );
}

export default App;
