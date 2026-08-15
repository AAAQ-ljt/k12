import UserManagement from '@/views/user/UserManagement';

export default function AdminAccount() {
  return (
    <div>
      <UserManagement roleType={0} />
    </div>
  );
}
