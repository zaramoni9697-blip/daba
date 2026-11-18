import './globals.css';

export const metadata = {
  title: 'Cloud AI Video Generator',
  description: 'Generate videos from text prompts with AI.'
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
